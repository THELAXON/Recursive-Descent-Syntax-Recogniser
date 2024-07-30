public class Generate extends AbstractGenerate {
    /**
     * Reports an error encountered during parsing.
     *
     * @param token              The token causing the error.
     * @param explanatoryMessage A message explaining the error.
     * @throws CompilationException Thrown to indicate a compilation error.
     */
    @Override
    public void reportError(Token token, String explanatoryMessage) throws CompilationException {
        String errorMessage = "Error at line " + token.lineNumber + " in " + explanatoryMessage;
        throw new CompilationException(errorMessage);
    }
}
