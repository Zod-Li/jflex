%%

%unicode 12.0
%public
%class UnicodePropList_ASCII_Hex_Digit_12_0

%type int
%standalone

%include ../../resources/common-unicode-all-binary-property-java

%%

\p{ASCII_Hex_Digit} { setCurCharPropertyValue(); }
[^] { }

<<EOF>> { printOutput(); return 1; }
