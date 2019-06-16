package com.dans.apps.bitsa.callbacks;

import com.dans.apps.bitsa.model.Entity;

public interface FragmentAdapterInteractionListener {

    void viewContent(Entity entity);
    void onDelete(Entity entity);
    void onUpdate(Entity entity);
}
