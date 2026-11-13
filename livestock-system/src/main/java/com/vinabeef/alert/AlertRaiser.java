package com.vinabeef.alert;

/**
 * The only alert capability detectors need. They depend on this narrow interface instead of the
 * full alert service with its queries and acknowledgements (ISP, DIP).
 */
public interface AlertRaiser {

    void raise(AlertDraft draft);
}
