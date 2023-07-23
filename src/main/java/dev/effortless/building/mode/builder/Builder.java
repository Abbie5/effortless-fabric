package dev.effortless.building.mode.builder;

import dev.effortless.building.Context;

public interface Builder extends Traceable, BlockPosCollector {

    int totalClicks(Context context);

}
