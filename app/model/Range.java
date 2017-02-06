package model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * Created by mahabaleshwar on 2/6/2017.
 */
@Embedded
public class Range {
    @Embedded
    private StartEnd start;
    @Embedded
    private StartEnd end;

    public Range(){

    }

    public Range(StartEnd start, StartEnd end) {
        this.start = start;
        this.end = end;
    }

    public Range(JsonNode range) {
        this.start = new StartEnd();
        this.end = new StartEnd();
        if(range.has("start")) {
            JsonNode s = range.get("start");
            if(s.has("row"))
                this.start.setRow(s.get("row").asText());
            if(s.has("column"))
                this.start.setColumn(s.get("column").asText());
        }
        if(range.has("end")) {
            JsonNode e = range.get("end");
            if(e.has("row"))
                this.end.setRow(e.get("row").asText());
            if(e.has("column"))
                this.end.setColumn(e.get("column").asText());
        }
    }

    public StartEnd getStart() {
        return start;
    }

    public void setStart(StartEnd start) {
        this.start = start;
    }

    public StartEnd getEnd() {
        return end;
    }

    public void setEnd(StartEnd end) {
        this.end = end;
    }

    public ObjectNode searalize() {
        ObjectNode range = Json.newObject();
        range.put("start", this.getStart().searalize());
        range.put("end", this.getEnd().searalize());
        return range;
    }
}

@Embedded
class StartEnd {
    private String row;
    private String column;

    public StartEnd() {
    }

    public StartEnd(String row, String column) {
        this.row = row;
        this.column = column;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public ObjectNode searalize() {
        ObjectNode o = Json.newObject();
        o.put("row", this.getRow());
        o.put("column", this.getRow());
        return o;
    }
}