let computeIndentLevel = function (line, tabSize) {
    var indent = 0;
    var i = 0;
    var len = line.length;
    while (i < len) {
        var chCode = line.charCodeAt(i);
        if (chCode === 32 /* Space */) {
            indent++;
        } else if (chCode === 9 /* Tab */) {
            indent = indent - indent % tabSize + tabSize;
        } else {
            break;
        }
        i++;
    }
    if (i === len) {
        return -1; // line only consists of whitespace
    }
    return indent;
}

class RangesCollector {
    constructor(foldingRangesLimit) {
        this._startIndexes = [];
        this._endIndexes = [];
        this._indentOccurrences = [];
        this._length = 0;
        this._foldingRangesLimit = foldingRangesLimit;
    }

    insertFirst(startLineNumber, endLineNumber, indent) {
        if (startLineNumber > 0xFFFFFF || endLineNumber > 0xFFFFFF) {
            return;
        }
        var index = this._length;
        this._startIndexes[index] = startLineNumber;
        this._endIndexes[index] = endLineNumber;
        this._length++;
        if (indent < 1000) {
            this._indentOccurrences[indent] = (this._indentOccurrences[indent] || 0) + 1;
        }
    }

    toIndentRanges(model) {
        var values = [];
        if (this._length <= this._foldingRangesLimit) {
            // reverse and create arrays of the exact length
            var startIndexes = new Uint32Array(this._length);
            var endIndexes = new Uint32Array(this._length);
            for (var i = this._length - 1, k = 0; i >= 0; i--, k++) {
                values.push({
                    start: this._startIndexes[i],
                    end: this._endIndexes[i]
                });
            }
        } else {
            var entries = 0;
            var maxIndent = this._indentOccurrences.length;
            for (var i = 0; i < this._indentOccurrences.length; i++) {
                var n = this._indentOccurrences[i];
                if (n) {
                    if (n + entries > this._foldingRangesLimit) {
                        maxIndent = i;
                        break;
                    }
                    entries += n;
                }
            }
            var tabSize = model.getOptions().tabSize;
            // reverse and create arrays of the exact length
            var startIndexes = new Uint32Array(this._foldingRangesLimit);
            var endIndexes = new Uint32Array(this._foldingRangesLimit);
            for (var i = this._length - 1, k = 0; i >= 0; i--) {
                var startIndex = this._startIndexes[i];
                var lineContent = model.getLineContent(startIndex);
                var indent = computeIndentLevel(lineContent, tabSize);
                if (indent < maxIndent || (indent === maxIndent && entries++ < this._foldingRangesLimit)) {
                    values.push({
                        start: startIndex,
                        end: this._endIndexes[i]
                    });
                    k++;
                }
            }
        }
        return values;
    }
}

const FoldingRangeProvider = {
    provideFoldingRanges: (model, context) => {
        let tabSize = model.getOptions().tabSize;
        let result = new RangesCollector(5000);
        let previousRegions = [];
        let line = model.getLineCount() + 1;
        let endImport = -1;
        let startImport = -1;
        let imports = [];
        previousRegions.push({indent: -1, endAbove: line, line: line});
        for (let line_1 = model.getLineCount(); line_1 > 0; line_1--) {
            let lineContent = model.getLineContent(line_1);
            if (lineContent.startsWith('import') || lineContent.trim().startsWith('import')) {
                if (endImport == -1) {
                    endImport = line_1;
                } else {
                    startImport = line_1
                }
            } else {
                if (startImport > -1 && endImport > -1) {
                    imports.push({
                        start: startImport,
                        end: endImport,
                        kind: monaco.languages.FoldingRangeKind.Imports
                    });
                }
                startImport = -1;
                endImport = -1;
            }
            let indent = computeIndentLevel(lineContent, tabSize);
            let previous = previousRegions[previousRegions.length - 1];
            if (indent === -1) {
                continue; // only whitespace
            }
            if (previous.indent > indent) {
                // discard all regions with larger indent
                do {
                    previousRegions.pop();
                    previous = previousRegions[previousRegions.length - 1];
                } while (previous.indent > indent);
                // new folding range
                var endLineNumber = previous.endAbove - 1;
                if (endLineNumber - line_1 >= 1) { // needs at east size 1
                    result.insertFirst(line_1, endLineNumber, indent);
                }
            }
            if (previous.indent === indent) {
                previous.endAbove = line_1;
            } else { // previous.indent < indent
                // new region with a bigger indent
                previousRegions.push({indent: indent, endAbove: line_1, line: line_1});
            }
        }
        if (startImport > -1 && endImport > -1) {
            imports.push({
                start: startImport,
                end: endImport,
                kind: monaco.languages.FoldingRangeKind.Imports
            });
        }
        return imports.concat(result.toIndentRanges(model));
    }
}

export default FoldingRangeProvider;