package com.inalogy.midpoint.connectors.filtertranslator;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;

public class SshFilterTranslator extends AbstractFilterTranslator<SshFilter> {
    private static final Log LOG = Log.getLog(SshFilterTranslator.class);

    @Override
    protected SshFilter createEqualsExpression(EqualsFilter filter, boolean not) {
        LOG.ok("createEqualsExpression, filter: {0}, not: {1}", filter, not);

        if (not) {
            return null;            // not supported
        }
        Attribute attr = filter.getAttribute();
        LOG.ok("attr.getName:  {0}, attr.getValue: {1}, Uid.NAME: {2}, Name.NAME: {3}", attr.getName(), attr.getValue(), Uid.NAME, Name.NAME);
        if (Uid.NAME.equals(attr.getName())) {
            SshFilter lookingFor = new SshFilter();
            lookingFor.byUid = String.valueOf(attr.getValue().get(0));
            return lookingFor;
        }
        else if (Name.NAME.equals(attr.getName())) {
            SshFilter lookingFor = new SshFilter();
            lookingFor.byName = String.valueOf(attr.getValue().get(0));
            return lookingFor;
        }
        return null;            // not supported
    }
}
