package com.inalogy.midpoint.connectors.ssh.filter;

import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class SshFilterTranslatorTest {

    @Test
    public void testCreateEqualsExpression_byUid() {
        SshFilterTranslator translator = new SshFilterTranslator();
        EqualsFilter filter = new EqualsFilter(AttributeBuilder.build(Uid.NAME, "guid-123"));

        SshFilter result = translator.createEqualsExpression(filter, false);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.byUid, "guid-123");
        Assert.assertNull(result.byName);
    }

    @Test
    public void testCreateEqualsExpression_byName() {
        SshFilterTranslator translator = new SshFilterTranslator();
        EqualsFilter filter = new EqualsFilter(AttributeBuilder.build(Name.NAME, "user@test.com"));

        SshFilter result = translator.createEqualsExpression(filter, false);

        Assert.assertNotNull(result);
        Assert.assertNull(result.byUid);
        Assert.assertEquals(result.byName, "user@test.com");
    }

    @Test
    public void testCreateEqualsExpression_notFilter_returnsNull() {
        SshFilterTranslator translator = new SshFilterTranslator();
        EqualsFilter filter = new EqualsFilter(AttributeBuilder.build(Uid.NAME, "guid-123"));

        SshFilter result = translator.createEqualsExpression(filter, true);

        Assert.assertNull(result);
    }

    @Test
    public void testCreateEqualsExpression_unknownAttribute_returnsNull() {
        SshFilterTranslator translator = new SshFilterTranslator();
        EqualsFilter filter = new EqualsFilter(AttributeBuilder.build("email", "user@test.com"));

        SshFilter result = translator.createEqualsExpression(filter, false);

        Assert.assertNull(result);
    }
}
