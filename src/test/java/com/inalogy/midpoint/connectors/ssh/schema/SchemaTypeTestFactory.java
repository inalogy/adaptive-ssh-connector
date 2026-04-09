package com.inalogy.midpoint.connectors.ssh.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchemaTypeTestFactory {

    public static SchemaTypeAttribute attr(String name, boolean required, boolean creatable,
                                           boolean updateable, boolean multivalued,
                                           String dataType, boolean returnedByDefault, boolean readable) {
        return new SchemaTypeAttribute(name, required, creatable, updateable, multivalued, dataType, returnedByDefault, readable);
    }

    public static SchemaTypeAttribute simpleStringAttr(String name) {
        return attr(name, false, true, true, false, "String", true, true);
    }

    public static SchemaTypeAttribute multiValuedStringAttr(String name) {
        return attr(name, false, true, true, true, "String", true, true);
    }

    public static SchemaTypeAttribute requiredStringAttr(String name) {
        return attr(name, true, true, true, false, "String", true, true);
    }

    public static SchemaType create(String icfsUid, String icfsName, String objectClassName,
                                    String createScript, String updateScript,
                                    String deleteScript, String searchScript,
                                    List<SchemaTypeAttribute> attributes) {
        return new SchemaType(icfsUid, icfsName, objectClassName, createScript, updateScript, deleteScript, searchScript, attributes);
    }

    public static SchemaType exchangeUserSchema() {
        List<SchemaTypeAttribute> attrs = new ArrayList<>();
        attrs.add(simpleStringAttr("email"));
        attrs.add(multiValuedStringAttr("emailAddresses"));
        return create("ExchangeGuid", "UserPrincipalName", "user",
                "/scripts/create.ps1", "/scripts/update.ps1",
                "/scripts/delete.ps1", "/scripts/search.ps1", attrs);
    }

    public static SchemaType exchangeGroupSchema() {
        List<SchemaTypeAttribute> attrs = new ArrayList<>();
        attrs.add(requiredStringAttr("groupName"));
        attrs.add(multiValuedStringAttr("members"));
        return create("uid", "icfsName", "group",
                "/scripts/grp/create.ps1", "/scripts/grp/update.ps1",
                "/scripts/grp/delete.ps1", "/scripts/grp/search.ps1", attrs);
    }

    public static SchemaType openBsdUserSchema() {
        List<SchemaTypeAttribute> attrs = new ArrayList<>();
        attrs.add(requiredStringAttr("login"));
        attrs.add(simpleStringAttr("fullName"));
        attrs.add(simpleStringAttr("type"));
        return create("uid", "uid", "user",
                "/home/svc/create.sh", "/home/svc/update.sh",
                "/home/svc/delete.sh", "/home/svc/search.sh", attrs);
    }

    public static SchemaType minimalSchema(String objectClassName) {
        return create("id", "name", objectClassName,
                "/scripts/create.sh", "/scripts/update.sh",
                "/scripts/delete.sh", "/scripts/search.sh", new ArrayList<>());
    }
}
