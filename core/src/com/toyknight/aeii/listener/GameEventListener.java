package com.toyknight.aeii.listener;

import com.toyknight.aeii.event.GameEvent;

/**
 * Created by toyknight on 4/18/2015.
 */
public interface GameEventListener {

    public void onEventDispatched(GameEvent event);

}
