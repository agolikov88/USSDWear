package com.agolikov.wearbalance;

import com.google.android.gms.wearable.Node;

/**
 * Created by agolikov on 04/10/14.
 */
public interface NodeStatusListener {

    void onNodeResolved(Node node);

    void onNodeUnresolved();

}
