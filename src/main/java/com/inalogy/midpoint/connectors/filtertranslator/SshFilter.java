package com.inalogy.midpoint.connectors.filtertranslator;
public class SshFilter {

    public String byUid;

    public String ExchangeGuidFlag = "-exchangeGuid";

    @Override
    public String toString() {
        return " " + ExchangeGuidFlag + " " + "\"" + byUid + "\"";
    }
}
