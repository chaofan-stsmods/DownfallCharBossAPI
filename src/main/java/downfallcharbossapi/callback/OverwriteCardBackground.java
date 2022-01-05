package downfallcharbossapi.callback;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public interface OverwriteCardBackground {
    default TextureAtlas.AtlasRegion getBackgroundAtlasRegion() {
        return null;
    }

    default Texture getBackgroundTexture() {
        return null;
    }
}
