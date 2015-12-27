package net.marmier.mediakey.sig;

import net.marmier.mediakey.metadata.MetaData;

/**
 * Added by raphael on 27.12.15.
 */
public interface SigGen {

    String createSig(MetaData meta);
}
