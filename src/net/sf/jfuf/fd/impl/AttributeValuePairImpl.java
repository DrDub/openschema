//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.3-b18-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2004.07.09 at 03:49:56 EDT 
//


package net.sf.jfuf.fd.impl;

public class AttributeValuePairImpl implements net.sf.jfuf.fd.AttributeValuePair, com.sun.xml.bind.JAXBObject, net.sf.jfuf.fd.impl.runtime.UnmarshallableObject, net.sf.jfuf.fd.impl.runtime.XMLSerializable, net.sf.jfuf.fd.impl.runtime.ValidatableObject
{

    protected java.lang.String _N;
    protected com.sun.xml.bind.util.ListImpl _V;
    protected java.lang.String _G;
    public final static java.lang.Class version = (net.sf.jfuf.fd.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (net.sf.jfuf.fd.AttributeValuePair.class);
    }

    public java.lang.String getN() {
        return _N;
    }

    public void setN(java.lang.String value) {
        _N = value;
    }

    protected com.sun.xml.bind.util.ListImpl _getV() {
        if (_V == null) {
            _V = new com.sun.xml.bind.util.ListImpl(new java.util.ArrayList());
        }
        return _V;
    }

    public java.util.List getV() {
        return _getV();
    }

    public java.lang.String getG() {
        return _G;
    }

    public void setG(java.lang.String value) {
        _G = value;
    }

    public net.sf.jfuf.fd.impl.runtime.UnmarshallingEventHandler createUnmarshaller(net.sf.jfuf.fd.impl.runtime.UnmarshallingContext context) {
        return new net.sf.jfuf.fd.impl.AttributeValuePairImpl.Unmarshaller(context);
    }

    public void serializeBody(net.sf.jfuf.fd.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_V == null)? 0 :_V.size());
        if ((((_V == null)? 0 :_V.size()) == 0)&&(_G!= null)) {
            context.startElement("http://jfuf.sf.net/FD", "G");
            context.endNamespaceDecls();
            context.endAttributes();
            try {
                context.text(((java.lang.String) _G), "G");
            } catch (java.lang.Exception e) {
                net.sf.jfuf.fd.impl.runtime.Util.handlePrintConversionException(this, e, context);
            }
            context.endElement();
        } else {
            if ((((_V == null)? 0 :_V.size())>= 1)&&(_G == null)) {
                while (idx2 != len2) {
                    context.startElement("http://jfuf.sf.net/FD", "V");
                    int idx_2 = idx2;
                    context.childAsURIs(((com.sun.xml.bind.JAXBObject) _V.get(idx_2 ++)), "V");
                    context.endNamespaceDecls();
                    int idx_3 = idx2;
                    context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _V.get(idx_3 ++)), "V");
                    context.endAttributes();
                    context.childAsBody(((com.sun.xml.bind.JAXBObject) _V.get(idx2 ++)), "V");
                    context.endElement();
                }
            }
        }
    }

    public void serializeAttributes(net.sf.jfuf.fd.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_V == null)? 0 :_V.size());
        context.startAttribute("", "N");
        try {
            context.text(((java.lang.String) _N), "N");
        } catch (java.lang.Exception e) {
            net.sf.jfuf.fd.impl.runtime.Util.handlePrintConversionException(this, e, context);
        }
        context.endAttribute();
        if (!((((_V == null)? 0 :_V.size()) == 0)&&(_G!= null))) {
            if ((((_V == null)? 0 :_V.size())>= 1)&&(_G == null)) {
                while (idx2 != len2) {
                    idx2 += 1;
                }
            }
        }
    }

    public void serializeURIs(net.sf.jfuf.fd.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_V == null)? 0 :_V.size());
        if (!((((_V == null)? 0 :_V.size()) == 0)&&(_G!= null))) {
            if ((((_V == null)? 0 :_V.size())>= 1)&&(_G == null)) {
                while (idx2 != len2) {
                    idx2 += 1;
                }
            }
        }
    }

    public java.lang.Class getPrimaryInterface() {
        return (net.sf.jfuf.fd.AttributeValuePair.class);
    }

    public com.sun.msv.verifier.DocumentDeclaration createRawValidator() {
        if (schemaFragment == null) {
            schemaFragment = com.sun.xml.bind.validator.SchemaDeserializer.deserialize((
 "\u00ac\u00ed\u0000\u0005sr\u0000\u001fcom.sun.msv.grammar.SequenceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.su"
+"n.msv.grammar.BinaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1t\u0000 Lcom/sun/msv/gra"
+"mmar/Expression;L\u0000\u0004exp2q\u0000~\u0000\u0002xr\u0000\u001ecom.sun.msv.grammar.Expressi"
+"on\u00f8\u0018\u0082\u00e8N5~O\u0002\u0000\u0002L\u0000\u0013epsilonReducibilityt\u0000\u0013Ljava/lang/Boolean;L\u0000\u000b"
+"expandedExpq\u0000~\u0000\u0002xpppsr\u0000\u001dcom.sun.msv.grammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0001ppsr\u0000\'com.sun.msv.grammar.trex.ElementPattern\u0000\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\tnameClasst\u0000\u001fLcom/sun/msv/grammar/NameClass;xr\u0000\u001ecom."
+"sun.msv.grammar.ElementExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002Z\u0000\u001aignoreUndeclaredAttr"
+"ibutesL\u0000\fcontentModelq\u0000~\u0000\u0002xq\u0000~\u0000\u0003pp\u0000sq\u0000~\u0000\u0000ppsr\u0000\u001bcom.sun.msv.g"
+"rammar.DataExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datat"
+"ype;L\u0000\u0006exceptq\u0000~\u0000\u0002L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000"
+"~\u0000\u0003ppsr\u0000#com.sun.msv.datatype.xsd.StringType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001Z\u0000\ris"
+"AlwaysValidxr\u0000*com.sun.msv.datatype.xsd.BuiltinAtomicType\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.datatype.xsd.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000"
+"xr\u0000\'com.sun.msv.datatype.xsd.XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnam"
+"espaceUrit\u0000\u0012Ljava/lang/String;L\u0000\btypeNameq\u0000~\u0000\u0015L\u0000\nwhiteSpacet"
+"\u0000.Lcom/sun/msv/datatype/xsd/WhiteSpaceProcessor;xpt\u0000 http://"
+"www.w3.org/2001/XMLSchemat\u0000\u0006stringsr\u00005com.sun.msv.datatype.x"
+"sd.WhiteSpaceProcessor$Preserve\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000,com.sun.msv.da"
+"tatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xp\u0001sr\u00000com.sun.msv."
+"grammar.Expression$NullSetExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003ppsr\u0000\u001bc"
+"om.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tlocalNameq\u0000~\u0000\u0015L\u0000\fnam"
+"espaceURIq\u0000~\u0000\u0015xpq\u0000~\u0000\u0019q\u0000~\u0000\u0018sq\u0000~\u0000\u0006ppsr\u0000 com.sun.msv.grammar.At"
+"tributeExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0002L\u0000\tnameClassq\u0000~\u0000\txq\u0000~\u0000\u0003sr\u0000\u0011j"
+"ava.lang.Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000psq\u0000~\u0000\rppsr\u0000\"com.sun.m"
+"sv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0012q\u0000~\u0000\u0018t\u0000\u0005QNamesr\u00005c"
+"om.sun.msv.datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001"
+"\u0002\u0000\u0000xq\u0000~\u0000\u001bq\u0000~\u0000\u001esq\u0000~\u0000\u001fq\u0000~\u0000)q\u0000~\u0000\u0018sr\u0000#com.sun.msv.grammar.Simple"
+"NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalNameq\u0000~\u0000\u0015L\u0000\fnamespaceURIq\u0000~\u0000\u0015xr\u0000"
+"\u001dcom.sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpt\u0000\u0004typet\u0000)http://"
+"www.w3.org/2001/XMLSchema-instancesr\u00000com.sun.msv.grammar.Ex"
+"pression$EpsilonExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003sq\u0000~\u0000$\u0001q\u0000~\u00003sq\u0000~\u0000"
+"-t\u0000\u0001Gt\u0000\u0015http://jfuf.sf.net/FDsr\u0000 com.sun.msv.grammar.OneOrMo"
+"reExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001ccom.sun.msv.grammar.UnaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L"
+"\u0000\u0003expq\u0000~\u0000\u0002xq\u0000~\u0000\u0003ppsq\u0000~\u0000\bpp\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\bpp\u0000sq\u0000~\u0000\u0006ppsq\u0000~\u00008q\u0000"
+"~\u0000%psq\u0000~\u0000\"q\u0000~\u0000%psr\u00002com.sun.msv.grammar.Expression$AnyString"
+"Expression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003q\u0000~\u00004q\u0000~\u0000Bsr\u0000 com.sun.msv.grammar"
+".AnyNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000.q\u0000~\u00003sq\u0000~\u0000-t\u0000!net.sf.jfuf.fd.A"
+"ttributeValuePairt\u0000+http://java.sun.com/jaxb/xjc/dummy-eleme"
+"ntssq\u0000~\u0000\u0006ppsq\u0000~\u0000\"q\u0000~\u0000%pq\u0000~\u0000&q\u0000~\u0000/q\u0000~\u00003sq\u0000~\u0000-t\u0000\u0001Vq\u0000~\u00007sq\u0000~\u0000\"p"
+"psq\u0000~\u0000\rppsr\u0000\"com.sun.msv.datatype.xsd.TokenType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq"
+"\u0000~\u0000\u0011q\u0000~\u0000\u0018t\u0000\u0005tokenq\u0000~\u0000+\u0001q\u0000~\u0000\u001esq\u0000~\u0000\u001fq\u0000~\u0000Pq\u0000~\u0000\u0018sq\u0000~\u0000-t\u0000\u0001Nt\u0000\u0000sr\u0000"
+"\"com.sun.msv.grammar.ExpressionPool\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/"
+"Lcom/sun/msv/grammar/ExpressionPool$ClosedHash;xpsr\u0000-com.sun"
+".msv.grammar.ExpressionPool$ClosedHash\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\r"
+"streamVersionL\u0000\u0006parentt\u0000$Lcom/sun/msv/grammar/ExpressionPool"
+";xp\u0000\u0000\u0000\t\u0001pq\u0000~\u0000!q\u0000~\u0000Hq\u0000~\u0000?q\u0000~\u0000<q\u0000~\u0000:q\u0000~\u0000\u0005q\u0000~\u0000\u0007q\u0000~\u0000>q\u0000~\u0000\fx"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends net.sf.jfuf.fd.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(net.sf.jfuf.fd.impl.runtime.UnmarshallingContext context) {
            super(context, "---------");
        }

        protected Unmarshaller(net.sf.jfuf.fd.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return net.sf.jfuf.fd.impl.AttributeValuePairImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  4 :
                        attIdx = context.getAttribute("", "N");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        break;
                    case  3 :
                        if (("G" == ___local)&&("http://jfuf.sf.net/FD" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 7;
                            return ;
                        }
                        if (("V" == ___local)&&("http://jfuf.sf.net/FD" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 4;
                            return ;
                        }
                        break;
                    case  0 :
                        attIdx = context.getAttribute("", "N");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            eatText1(v);
                            state = 3;
                            continue outer;
                        }
                        break;
                    case  6 :
                        if (("V" == ___local)&&("http://jfuf.sf.net/FD" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 4;
                            return ;
                        }
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                }
                super.enterElement(___uri, ___local, ___qname, __atts);
                break;
            }
        }

        private void eatText1(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _N = com.sun.xml.bind.WhiteSpaceProcessor.collapse(value);
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        public void leaveElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  5 :
                        if (("V" == ___local)&&("http://jfuf.sf.net/FD" == ___uri)) {
                            context.popAttributes();
                            state = 6;
                            return ;
                        }
                        break;
                    case  4 :
                        attIdx = context.getAttribute("", "N");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  8 :
                        if (("G" == ___local)&&("http://jfuf.sf.net/FD" == ___uri)) {
                            context.popAttributes();
                            state = 6;
                            return ;
                        }
                        break;
                    case  0 :
                        attIdx = context.getAttribute("", "N");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            eatText1(v);
                            state = 3;
                            continue outer;
                        }
                        break;
                    case  6 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                }
                super.leaveElement(___uri, ___local, ___qname);
                break;
            }
        }

        public void enterAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  4 :
                        if (("N" == ___local)&&("" == ___uri)) {
                            _getV().add(((net.sf.jfuf.fd.impl.AttributeValuePairImpl) spawnChildFromEnterAttribute((net.sf.jfuf.fd.impl.AttributeValuePairImpl.class), 5, ___uri, ___local, ___qname)));
                            return ;
                        }
                        break;
                    case  0 :
                        if (("N" == ___local)&&("" == ___uri)) {
                            state = 1;
                            return ;
                        }
                        break;
                    case  6 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                }
                super.enterAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void leaveAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  4 :
                        attIdx = context.getAttribute("", "N");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  2 :
                        if (("N" == ___local)&&("" == ___uri)) {
                            state = 3;
                            return ;
                        }
                        break;
                    case  0 :
                        attIdx = context.getAttribute("", "N");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            eatText1(v);
                            state = 3;
                            continue outer;
                        }
                        break;
                    case  6 :
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                }
                super.leaveAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void handleText(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                try {
                    switch (state) {
                        case  4 :
                            attIdx = context.getAttribute("", "N");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            break;
                        case  1 :
                            eatText1(value);
                            state = 2;
                            return ;
                        case  7 :
                            eatText2(value);
                            state = 8;
                            return ;
                        case  0 :
                            attIdx = context.getAttribute("", "N");
                            if (attIdx >= 0) {
                                final java.lang.String v = context.eatAttribute(attIdx);
                                eatText1(v);
                                state = 3;
                                continue outer;
                            }
                            break;
                        case  6 :
                            revertToParentFromText(value);
                            return ;
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

        private void eatText2(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _G = value;
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

    }

}
