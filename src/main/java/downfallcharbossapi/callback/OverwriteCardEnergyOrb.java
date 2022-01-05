package downfallcharbossapi.callback;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public interface OverwriteCardEnergyOrb {
    default TextureAtlas.AtlasRegion getEnergyOrbAtlasRegion() {
        return null;
    }

    default Texture getEnergyOrbTexture() {
        return null;
    }
}
