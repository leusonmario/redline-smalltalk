/* Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution */
package st.redline.compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodAnalyser extends Analyser {

	private final Map<String, VariableName> argumentRegistry;

	public MethodAnalyser(String className, String packageName, int countOfArguments, boolean isClassMethod, Analyser containingAnalyser) {
		super(className, packageName, countOfArguments, isClassMethod, containingAnalyser);
		blockSequence(containingAnalyser.blockSequence() + 1);
		argumentRegistry = new HashMap<String, VariableName>();
	}

	protected void initialize() {
		classBytecodeWriter = new MethodBytecodeWriter(className, packageName, countOfArguments);
	}

	public int methodTemporariesCount() {
		return 0;
	}

	public boolean continueMethodVisit() {
		return true;
	}

	protected boolean isMethodArgument(String value) {
//		System.out.println("isMethodArgument() " + value + " " + isLocalMethodArgument(value) + " " + isOuterContextMethodArgument(value));
		return isLocalMethodArgument(value) || isOuterContextMethodArgument(value);
	}

	protected boolean isLocalMethodArgument(String value) {
		return argumentRegistry.containsKey(value);
	}

	protected void loadMethodArgument(int line, String value, boolean isLocal) {
//		System.out.println("loadMethodArgument() " + value + " " + isLocal);
		if (isLocal) {
			VariableName variableName = methodArgument(value);
			classBytecodeWriter.stackPushLocal(variableName.index + 3);  // 0 = method, 1 = receiver, 2 = this context,, then local fields.
		} else {
			VariableName variableName = outerContextMethodArgument(value);
			classBytecodeWriter.callOuterContextMethodArgumentAt(variableName.index, line);
		}
	}

	protected VariableName methodArgument(String name) {
		return argumentRegistry.get(name);
	}

	public void visit(BlockVariableName blockVariableName, String value, int line) {
		throw new IllegalStateException("BlockAnalyser should be handling this.");
	}

	public void visit(InstanceMethod instanceMethod) {
		classBytecodeWriter.openClass();
	}

	public void visitEnd(InstanceMethod instanceMethod) {
		if (instanceMethod.isEmpty())
			classBytecodeWriter.stackPushReceiver(instanceMethod.line());
		classBytecodeWriter.closeClass();
	}

	public void visit(ClassMethod classMethod) {
		classBytecodeWriter.openClass();
	}

	public void visitEnd(ClassMethod classMethod) {
		if (classMethod.isEmpty())
			classBytecodeWriter.stackPushReceiver(classMethod.line());
		classBytecodeWriter.closeClass();
	}

	public void visit(UnarySelectorMessagePattern unarySelectorMessagePattern, String value, int line) {
	}

	public void visit(BinarySelectorMessagePattern binarySelectorMessagePattern, String binarySelector, int binarySelectorLine, VariableName variableName) {
		registerMethodArgument(variableName);
	}

	public void visit(KeywordMessagePattern keywordMessagePattern, String keywords, int keywordLine, List<VariableName> variableNames) {
		registerMethodArguments(variableNames);
	}

	private void registerMethodArguments(List<VariableName> variableNames) {
		for (VariableName variableName : variableNames)
			registerMethodArgument(variableName);
	}

	protected void registerMethodArgument(VariableName variableName) {
		String name = variableName.value();
		if (isMethodArgument(name))
			throw new IllegalStateException("Duplicate method argument name '" + name + "'.");
		argumentRegistry.put(name, variableName);
	}
}
