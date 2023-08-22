package org.ssssssss.magicapi.nebula.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

public class NebulaJsonBody {
    private List<NebulaError> errors;
    private List<Result> results;

    public List<NebulaError> getErrors() {
        return errors;
    }

    public void setErrors(List<NebulaError> errors) {
        this.errors = errors;
    }


    public int getErrorCode() {
        return this.errors.get(0).getCode();
    }

    public String getErrorMsg() {
        return this.errors.get(0).getMessage();
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }


    public static class NebulaError {
        private int code;
        private String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class Result {
        @JsonProperty("spaceName")
        private String spaceName;
        private List<Data> data;
        private List<String> columns;
        private NebulaError errors;
        @JsonProperty("latencyInUs")
        private long latencyInUs;

        public String getSpaceName() {
            return spaceName;
        }

        public void setSpaceName(String spaceName) {
            this.spaceName = spaceName;
        }

        public List<Data> getData() {
            return data;
        }

        public void setData(List<Data> data) {
            this.data = data;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public NebulaError getErrors() {
            return errors;
        }

        public void setErrors(NebulaError errors) {
            this.errors = errors;
        }

        public long getLatencyInUs() {
            return latencyInUs;
        }

        public void setLatencyInUs(long latencyInUs) {
            this.latencyInUs = latencyInUs;
        }
    }

    public static class Data {
        private List<List<Element>> meta;
        private List<List<HashMap<String, Object>>> row;

        public List<List<Element>> getMeta() {
            return meta;
        }

        public void setMeta(List<List<Element>> meta) {
            this.meta = meta;
        }

        public List<List<HashMap<String, Object>>> getRow() {
            return row;
        }

        public void setRow(List<List<HashMap<String, Object>>> row) {
            this.row = row;
        }
    }
}



