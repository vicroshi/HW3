tools:
	java -jar ./jars/jtb132di.jar -te minijava.jj
	java -jar ./jars/javacc5.jar minijava-jtb.jj
	javac -cp ./javacc:./ Main.java -d ./classes_out

main_extra:
	javac -cp ./javacc:./ Main.java -d ./classes_out
	java -cp :javacc Main minijava-examples-new/minijava-extra/*.java

main_normal:
	javac -cp ./javacc:./ Main.java -d ./classes_out
	java -cp :javacc Main minijava-examples-new/*.java

main_extra_error:
	javac -cp ./javacc:./ Main.java -d ./classes_out
	java -cp :javacc Main minijava-examples-new/minijava-error-extra/*.java

main_baziotis:
	javac -cp :javacc Main.java -d ./classes_out
	java -cp :javacc Main minijava-testsuite/*.java

main_progressive:
	javac -cp :javacc Main.java -d ./classes_out
	java -cp :javacc Main progressive/*.java

clean:
#	rm -rf *.class *~
#	rm -rf syntaxtree/*.class
#	rm -rf visitor/*.class
#	rm -rf javacc/*.class
	rm -rf classes_out/*