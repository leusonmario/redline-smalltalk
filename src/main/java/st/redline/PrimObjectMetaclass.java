/* Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution */
package st.redline;

// Provides a way of constructing CLASSES. A Metaclass is a class who's instances are CLASSES of CLASSES.
// Each class is an instance of its own Metaclass.
// Call basicSubclassOf: to get a subclass of Metaclass,
// Call basicCreate:... to create the Metaclass' sole instance.

import java.util.Hashtable;
import java.util.Map;

public class PrimObjectMetaclass extends PrimObjectClass {

	static final int DEFAULT_ATTRIBUTE_COUNT = 1;  // for name, superclass etc etc
	static final int NAME_INDEX = PrimObjectClass.SUPERCLASS_INDEX + 1;
	static final PrimObjectMetaclass METACLASS;

	static {
		PrimObject.BOOTSTRAPPING = true;
		METACLASS = basicSubclassOf(null).basicCreate("Metaclass", PrimObject.PRIM_NIL, "", "", "", "");
	}
	static Map<String, String> IMPORTS = new Hashtable<String, String>();

	Map<String, String> imports;

	public static PrimObjectMetaclass basicSubclassOf(PrimObjectMetaclass superMeta) {
		PrimObjectMetaclass newMetaclass = new PrimObjectMetaclass();
		newMetaclass.superclass(superMeta);
		return newMetaclass;
	}

	public PrimObjectMetaclass basicCreate(String name, PrimObject superclass, String instVarNames, String classVarNames,
	                                       String poolDictionaries, String classInstVarNames) {
		return basicCreate(string(name), superclass, string(instVarNames), string(classVarNames),
				string(poolDictionaries), string(classInstVarNames));
	}

	public PrimObjectMetaclass basicCreate(PrimObject name, PrimObject superclass, PrimObject instVarNames, PrimObject classVarNames,
	                                       PrimObject poolDictionaries, PrimObject classInstVarNames) {
		PrimObjectMetaclass newClass = new PrimObjectMetaclass();
		newClass.cls(this);
		newClass.superclass(superclass);
		newClass.name(name);
		// TODO.jcl take other parameters into account.
        if (containsString(instVarNames))
            newClass.perform(instVarNames, "instanceVariableNames:");
        if (containsString(classVarNames))
            newClass.perform(classVarNames, "classVariableNames:");
		return newClass;
	}

    boolean containsString(PrimObject object) {
        return object != null && object.javaValue() != null && String.valueOf(object.javaValue()).length() > 0;
    }

	public String toString() {
		if (name().javaValue == null)
			return super.toString();
		return (String) name().javaValue;
	}

	PrimObject name() {
		return attributes[NAME_INDEX];
	}

	PrimObjectMetaclass name(String name) {
		return name(string(name));
	}

	PrimObjectMetaclass name(PrimObject name) {
		attributes[NAME_INDEX] = name;
		return this;
	}

	public void addClassVariableNamed(String name) {
		System.out.println("addClassVariableNamed() " + name + " @ " + nextVariableIndex + " to " + this);
		if (hasClassVariableNamed(name))
			throw new IllegalStateException("Class variable '" + name + "' already defined.");
		((PrimObjectClass) cls()).variableIndexes().put(name, nextVariableIndex);
        expandAttributes();
        nextVariableIndex++;
    }

    void expandAttributes() {
        PrimObject[] to = new PrimObject[attributes.length + 1];
        System.arraycopy(attributes, 0, to, 0, attributes.length);
        attributes = to;
        attributes[attributes.length - 1] = BOOTSTRAPPING ? PRIM_NIL : NIL;
    }

    public PrimObjectMetaclass() {
		this(0);
	}

	PrimObjectMetaclass(int basicSize) {
		super(basicSize + DEFAULT_ATTRIBUTE_COUNT);
		cls(METACLASS);
	}

	void bootstrap() {
		new Bootstrapper(METACLASS).bootstrap();
	}

	public String packageFor(String name) {
		String packageName;
		if (imports != null && (packageName = imports.get(name)) != null)
			return packageName;
		if (superclass() != null && (packageName = superclass().packageFor(name)) != null)
			return packageName;
		return IMPORTS.get(name);
	}

    public void packageAtPut(String name, String packageName) {
        System.out.println("packageAtPut() " + this + " at: " + name + " package: " + packageName);
        if (imports == null)
            imports = new Hashtable<String, String>();
        if (imports.containsKey(name))
            throw new IllegalStateException("'" + name + "' already registered to a package.");
        imports.put(name, packageName);
    }

	protected PrimObject _sendMessages_(PrimObject receiver, PrimContext context) {
		return this;
	}
}
