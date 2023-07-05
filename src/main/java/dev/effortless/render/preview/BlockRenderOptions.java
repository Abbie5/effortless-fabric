package dev.effortless.render.preview;

import dev.effortless.Effortless;

public enum BlockRenderOptions {
    OUTLINES("outlines"),
    //    BLOCK_TEX("block_tex"),
    DISSOLVE_SHADER("dissolve_shader");

    private final String name;

    BlockRenderOptions(String name) {
        this.name = name;
    }

    public String getNameKey() {
        return Effortless.MOD_ID + ".render.options." + name;
    }

}
