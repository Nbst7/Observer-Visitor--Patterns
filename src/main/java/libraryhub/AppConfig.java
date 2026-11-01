package libraryhub;

/** Settings of the whole system. Parsed once in Main and passed to the composition root. */
public record AppConfig(int producerThreads, int pairsPerProducer, int loanDays,
                        int lowStockThreshold, double shippingPerCopy) {

    public AppConfig {
        if (producerThreads < 1) {
            throw new IllegalArgumentException("producerThreads must be at least 1");
        }
        if (pairsPerProducer < 0 || loanDays < 1 || lowStockThreshold < 0 || shippingPerCopy < 0) {
            throw new IllegalArgumentException("Invalid configuration values");
        }
    }

    public static AppConfig defaults() {
        return new AppConfig(3, 500, 14, 1, 2.0);
    }

    /** Understands {@code --threads=N} and {@code --pairs=N}; everything else keeps its default. */
    public static AppConfig parse(String[] args) {
        AppConfig base = defaults();
        int threads = base.producerThreads();
        int pairs = base.pairsPerProducer();
        for (String arg : args) {
            if (arg.startsWith("--threads=")) {
                threads = Integer.parseInt(arg.substring("--threads=".length()));
            } else if (arg.startsWith("--pairs=")) {
                pairs = Integer.parseInt(arg.substring("--pairs=".length()));
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        return new AppConfig(threads, pairs, base.loanDays(), base.lowStockThreshold(), base.shippingPerCopy());
    }
}
