package app.contactsonly;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.Assert.assertEquals;

/** Guards platform assumptions that would otherwise silently change which calls are rejected. */
public class PlatformContractTest {
    private Document manifest() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new File("src/main/AndroidManifest.xml"));
    }

    @Test public void contactsFilteringAndOfflinePrivacyRequireNoPermissions() throws Exception {
        Document manifest = manifest();
        assertEquals("Adding READ_CONTACTS would cause saved contacts to be screened too",
                0, manifest.getElementsByTagName("uses-permission").getLength());
        assertEquals(0, manifest.getElementsByTagName("uses-permission-sdk-23").getLength());
    }

    @Test public void onlyAndroidCanBindTheScreeningService() throws Exception {
        Document manifest = manifest();
        assertEquals(1, manifest.getElementsByTagName("service").getLength());
        Element service = (Element) manifest.getElementsByTagName("service").item(0);
        assertEquals("android.permission.BIND_SCREENING_SERVICE",
                service.getAttributeNS("http://schemas.android.com/apk/res/android", "permission"));
        assertEquals("true", service.getAttributeNS("http://schemas.android.com/apk/res/android", "exported"));
    }
}
