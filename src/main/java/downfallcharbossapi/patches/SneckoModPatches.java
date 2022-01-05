package downfallcharbossapi.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import downfallcharbossapi.DownfallCharBossApi;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import sneckomod.SneckoMod;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class SneckoModPatches {

    @SpirePatch(clz = SneckoMod.class, method = "receiveEditCards")
    @SpirePatch(clz = SneckoMod.class, method = "resetUnknownsLists")
    @SpirePatch(clz = SneckoMod.class, method = "findAWayToTriggerThisAtGameStart")
    public static class SneckoModRemoveCardColorPatch {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    // CardColor.values
                    if (m.getMethodName().equals("values")) {
                        m.replace("{ $_ = " + SneckoModPatches.class.getCanonicalName() + ".removeBladeGunner($proceed($$)); }");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = SneckoMod.class, method = "autoAddCards")
    public static class SneckoModPatch2 {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(CodeSource.class.getCanonicalName())
                            && m.getMethodName().equals("getLocation")) {
                        m.replace("{ $_ = " + SneckoModPatches.class.getCanonicalName() + ".getSneckoModLocation(); }");
                    }
                }
            };
        }
    }

    public static URL getSneckoModLocation() {
        URL url = getLocation(SneckoMod.class);
        DownfallCharBossApi.logger.info("Patch snecko mod url = " + url);
        return url;
    }

    public static AbstractCard.CardColor[] removeBladeGunner(AbstractCard.CardColor[] cardColors) {
        return Stream.of(cardColors).filter(c -> !DownfallCharBossApi.excludedCardColor.contains(c)).toArray(AbstractCard.CardColor[]::new);
    }

    // Copied from stackoverflow.
    private static URL getLocation(final Class<?> c) {
        if (c == null) return null; // could not load the class

        // try the easy way first
        try {
            final URL codeSourceLocation =
                    c.getProtectionDomain().getCodeSource().getLocation();
            if (codeSourceLocation != null) return codeSourceLocation;
        } catch (final SecurityException e) {
            // NB: Cannot access protection domain.
        } catch (final NullPointerException e) {
            // NB: Protection domain or code source is null.
        }

        // NB: The easy way failed, so we try the hard way. We ask for the class
        // itself as a resource, then strip the class's path from the URL string,
        // leaving the base path.

        // get the class's raw resource path
        final URL classResource = c.getResource(c.getSimpleName() + ".class");
        if (classResource == null) return null; // cannot find class resource

        final String url = classResource.toString();
        final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
        if (!url.endsWith(suffix)) return null; // weird URL

        // strip the class's path from the URL string
        final String base = url.substring(0, url.length() - suffix.length());

        String path = base;

        // remove the "jar:" prefix and "!/" suffix, if present
        if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

        try {
            return new URL(path);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
