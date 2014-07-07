package equa.code.operations;

public enum AccessModifier {

    PUBLIC("+"), NAMESPACE("~"), PROTECTED("#"), PRIVATE("-");
    private String abbreviation;

    private AccessModifier(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
