package equa.meta.requirements;

/**
 *
 * @author FrankP
 */
public enum VerifyMethod {

    UNDEFINED,
    /**
     * this requirement doesn't need a test
     */
    NONE,
    /**
     * this requirement needs a test
     */
    TEST,
    /**
     * this requirement needs a demonstration
     */
    DEMONSTRATION,
    /**
     * this requirement needs an inspection by an expert
     */
    INSPECTION,
    /**
     * this requirement needs an analysis by an expert
     */
    ANALYSIS;
}
