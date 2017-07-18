package contactBook.dto;

import contactBook.model.FieldValue;

public class FieldValueDto {
    private int id;
    private int userId;
    private int fieldId;
    private String fieldName;
    private String value;

    public FieldValueDto() {
    }

    public FieldValueDto(FieldValue fieldValue) {
        this.id = fieldValue.getId();
        this.userId = fieldValue.getUser().getId();
        this.fieldId = fieldValue.getField().getId();
        this.fieldName = fieldValue.getField().getName();
        this.value = fieldValue.getValue();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
