package uk.local.casualty;
final class BridgePolicy {
    static final String EASTENDERS_SIGNER="6f85ae49982e0b38b9f134b6882fdbe4da6091051f828fb980c5a03fff439535";
    static final String CASUALTY_SIGNER="d198e64f5cce0ccc77201bebd41db0283832be7b05476e26c2370b72d018a7ef";
    static boolean accepts(String pkg,String signer) {
        return ("uk.local.eastenders".equals(pkg) && EASTENDERS_SIGNER.equals(signer))
            || ("uk.local.casualty".equals(pkg) && CASUALTY_SIGNER.equals(signer));
    }
}
