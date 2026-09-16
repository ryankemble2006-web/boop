package local.johnnycastaway.shield;
/** Healthy HA polling is quick; provider outages retain the old slow retry cadence. */
final class JohnnyPollPolicy {
 private static final long HEALTHY_POLL_MS=250L;
 private static final int UNAVAILABLE_SKIP_POLLS=39;
 private JohnnyPollPolicy(){}
 static long healthyPollMs(){return HEALTHY_POLL_MS;}
 static int unavailableSkipPolls(){return UNAVAILABLE_SKIP_POLLS;}
 static long unavailableRetryMs(){return (UNAVAILABLE_SKIP_POLLS+1L)*HEALTHY_POLL_MS;}
}
