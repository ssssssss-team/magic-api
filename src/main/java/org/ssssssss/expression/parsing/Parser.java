package org.ssssssss.expression.parsing;


import org.ssssssss.expression.ExpressionError;
import org.ssssssss.expression.ExpressionTemplate;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;


/** Parses a {@link Source} into a {@link ExpressionTemplate}. The implementation is a simple recursive descent parser with a lookahead of
 * 1. **/
public class Parser {

	/** Parses a {@link Source} into a {@link ExpressionTemplate}. **/
	public static List<Ast.Node> parse (String source) {
		List<Ast.Node> nodes = new ArrayList<Ast.Node>();
		TokenStream stream = new TokenStream(new Tokenizer().tokenize(source));
		while (stream.hasMore()) {
			nodes.add(parseStatement(stream));
		}
		return nodes;
	}

	/** Parse a statement, which may either be a text block, if statement, for statement, while statement, macro definition,
	 * include statement or an expression. **/
	private static Ast.Node parseStatement (TokenStream tokens) {
		Ast.Node result = null;

		if (tokens.match(TokenType.TextBlock, false)) {
			result = new Ast.Text(tokens.consume().getSpan());
		} else {
			result = parseExpression(tokens);
		}

		// consume semi-colons as statement delimiters
		while (tokens.match(";", true)) {
			;
		}

		return result;
	}


	private static Ast.Expression parseExpression (TokenStream stream) {
		return parseTernaryOperator(stream);
	}

	private static Ast.Expression parseTernaryOperator (TokenStream stream) {
		Ast.Expression condition = parseBinaryOperator(stream, 0);
		if (stream.match(TokenType.Questionmark, true)) {
			Ast.Expression trueExpression = parseTernaryOperator(stream);
			stream.expect(TokenType.Colon);
			Ast.Expression falseExpression = parseTernaryOperator(stream);
			return new Ast.TernaryOperation(condition, trueExpression, falseExpression);
		} else {
			return condition;
		}
	}

	private static final TokenType[][] binaryOperatorPrecedence = new TokenType[][] {new TokenType[] {TokenType.Assignment},
		new TokenType[] {TokenType.Or, TokenType.And, TokenType.Xor}, new TokenType[] {TokenType.Equal, TokenType.NotEqual},
		new TokenType[] {TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual}, new TokenType[] {TokenType.Plus, TokenType.Minus},
		new TokenType[] {TokenType.ForwardSlash, TokenType.Asterisk, TokenType.Percentage}};

	private static Ast.Expression parseBinaryOperator (TokenStream stream, int level) {
		int nextLevel = level + 1;
		Ast.Expression left = nextLevel == binaryOperatorPrecedence.length ? parseUnaryOperator(stream) : parseBinaryOperator(stream, nextLevel);

		TokenType[] operators = binaryOperatorPrecedence[level];
		while (stream.hasMore() && stream.match(false, operators)) {
			Token operator = stream.consume();
			Ast.Expression right = nextLevel == binaryOperatorPrecedence.length ? parseUnaryOperator(stream) : parseBinaryOperator(stream, nextLevel);
			left = new Ast.BinaryOperation(left, operator, right);
		}

		return left;
	}

	private static final TokenType[] unaryOperators = new TokenType[] {TokenType.Not, TokenType.Plus, TokenType.Minus};

	private static Ast.Expression parseUnaryOperator (TokenStream stream) {
		if (stream.match(false, unaryOperators)) {
			return new Ast.UnaryOperation(stream.consume(), parseUnaryOperator(stream));
		} else {
			if (stream.match(TokenType.LeftParantheses, true)) {
				Ast.Expression expression = parseExpression(stream);
				stream.expect(TokenType.RightParantheses);
				return expression;
			} else {
				return parseAccessOrCallOrLiteral(stream);
			}
		}
	}

	private static Ast.Expression parseAccessOrCallOrLiteral (TokenStream stream) {
		if (stream.match(TokenType.Identifier, false)) {
			return parseAccessOrCall(stream,TokenType.Identifier);
		} else if (stream.match(TokenType.LeftCurly, false)) {
			return parseMapLiteral(stream);
		} else if (stream.match(TokenType.LeftBracket, false)) {
			return parseListLiteral(stream);
		} else if (stream.match(TokenType.StringLiteral, false)) {
			if(stream.hasNext()){
				if(stream.next().getType() == TokenType.Period){
					stream.prev();
					return parseAccessOrCall(stream,TokenType.StringLiteral);
				}
				stream.prev();
			}
			
			return new Ast.StringLiteral(stream.expect(TokenType.StringLiteral).getSpan());
		} else if (stream.match(TokenType.BooleanLiteral, false)) {
			return new Ast.BooleanLiteral(stream.expect(TokenType.BooleanLiteral).getSpan());
		} else if (stream.match(TokenType.DoubleLiteral, false)) {
			return new Ast.DoubleLiteral(stream.expect(TokenType.DoubleLiteral).getSpan());
		} else if (stream.match(TokenType.FloatLiteral, false)) {
			return new Ast.FloatLiteral(stream.expect(TokenType.FloatLiteral).getSpan());
		} else if (stream.match(TokenType.ByteLiteral, false)) {
			return new Ast.ByteLiteral(stream.expect(TokenType.ByteLiteral).getSpan());
		} else if (stream.match(TokenType.ShortLiteral, false)) {
			return new Ast.ShortLiteral(stream.expect(TokenType.ShortLiteral).getSpan());
		} else if (stream.match(TokenType.IntegerLiteral, false)) {
			return new Ast.IntegerLiteral(stream.expect(TokenType.IntegerLiteral).getSpan());
		} else if (stream.match(TokenType.LongLiteral, false)) {
			return new Ast.LongLiteral(stream.expect(TokenType.LongLiteral).getSpan());
		} else if (stream.match(TokenType.CharacterLiteral, false)) {
			return new Ast.CharacterLiteral(stream.expect(TokenType.CharacterLiteral).getSpan());
		} else if (stream.match(TokenType.NullLiteral, false)) {
			return new Ast.NullLiteral(stream.expect(TokenType.NullLiteral).getSpan());
		} else {
			ExpressionError.error("Expected a variable, field, map, array, function or method call, or literal.", stream);
			return null; // not reached
		}
	}

	private static Ast.Expression parseMapLiteral (TokenStream stream) {
		Span openCurly = stream.expect(TokenType.LeftCurly).getSpan();

		List<Token> keys = new ArrayList<>();
		List<Ast.Expression> values = new ArrayList<>();
		while (stream.hasMore() && !stream.match("}", false)) {
			if(stream.match(TokenType.StringLiteral, false)){
				keys.add(stream.expect(TokenType.StringLiteral));
			}else{
				keys.add(stream.expect(TokenType.Identifier));
			}
			
			stream.expect(":");
			values.add(parseExpression(stream));
			if (!stream.match("}", false)) {
				stream.expect(TokenType.Comma);
			}
		}
		Span closeCurly = stream.expect("}").getSpan();
		return new Ast.MapLiteral(new Span(openCurly, closeCurly), keys, values);
	}

	private static Ast.Expression parseListLiteral (TokenStream stream) {
		Span openBracket = stream.expect(TokenType.LeftBracket).getSpan();

		List<Ast.Expression> values = new ArrayList<>();
		while (stream.hasMore() && !stream.match(TokenType.RightBracket, false)) {
			values.add(parseExpression(stream));
			if (!stream.match(TokenType.RightBracket, false)) {
				stream.expect(TokenType.Comma);
			}
		}

		Span closeBracket = stream.expect(TokenType.RightBracket).getSpan();
		return new Ast.ListLiteral(new Span(openBracket, closeBracket), values);
	}

	private static Ast.Expression parseAccessOrCall (TokenStream stream, TokenType tokenType) {
		//Span identifier = stream.expect(TokenType.Identifier);
		//Expression result = new VariableAccess(identifier);
		Span identifier = stream.expect(tokenType).getSpan();
		Ast.Expression result = tokenType == TokenType.StringLiteral ? new Ast.StringLiteral(identifier) :new Ast.VariableAccess(identifier);

		while (stream.hasMore() && stream.match(false, TokenType.LeftParantheses, TokenType.LeftBracket, TokenType.Period, TokenType.Lambda)) {

			// function or method call
			if (stream.match(TokenType.LeftParantheses, false)) {
				List<Ast.Expression> arguments = parseArguments(stream);
				Span closingSpan = stream.expect(TokenType.RightParantheses).getSpan();
				if (result instanceof Ast.VariableAccess || result instanceof Ast.MapOrArrayAccess) {
					result = new Ast.FunctionCall(new Span(result.getSpan(), closingSpan), result, arguments);
				} else if (result instanceof Ast.MemberAccess) {
					for (Ast.Expression expression : arguments) {
						if (expression instanceof Ast.LambdaAccess) {
							Ast.LambdaAccess lambdaAccess = (Ast.LambdaAccess) expression;
							lambdaAccess.setArrayLike((Ast.MemberAccess) result);
						}
					}
					Ast.MethodCall methodCall = new Ast.MethodCall(new Span(result.getSpan(), closingSpan), (Ast.MemberAccess) result, arguments);
					if ("map".equals(((Ast.MemberAccess) result).getName().getText())) {
						try {
							methodCall.setCachedMethod(ArrayLikeLambdaExecutor.class.getMethod("map", Object.class, Object[].class));
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
						methodCall.setCachedMethodStatic(true);
					}
					result = methodCall;
				} else {
					ExpressionError.error("Expected a variable, field or method.", stream);
				}
			}

			// map or array access
			else if (stream.match(TokenType.LeftBracket, true)) {
				Ast.Expression keyOrIndex = parseExpression(stream);
				Span closingSpan = stream.expect(TokenType.RightBracket).getSpan();
				result = new Ast.MapOrArrayAccess(new Span(result.getSpan(), closingSpan), result, keyOrIndex);
			}

			// field or method access
			else if (stream.match(TokenType.Period, true)) {
				identifier = stream.expect(TokenType.Identifier).getSpan();
				result = new Ast.MemberAccess(result, identifier);
			}

			else if (stream.match(TokenType.Lambda, true)) {
				Ast.Expression key = parseExpression(stream);
//				Span closingSpan = stream.expect(TokenType.RightParantheses).getSpan();
				result = new Ast.LambdaAccess(new Span(result.getSpan(), key.getSpan()), result, key);
			}
		}

		return result;
	}

	/** Does not consume the closing parentheses. **/
	private static List<Ast.Expression> parseArguments (TokenStream stream) {
		stream.expect(TokenType.LeftParantheses);
		List<Ast.Expression> arguments = new ArrayList<Ast.Expression>();
		while (stream.hasMore() && !stream.match(TokenType.RightParantheses, false)) {
			arguments.add(parseExpression(stream));
			if (!stream.match(TokenType.RightParantheses, false)) {
				stream.expect(TokenType.Comma);
			}
		}
		return arguments;
	}
}
