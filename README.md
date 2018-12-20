# FormulaParser
A Java library to parse Seamly2D/Valentina formulas. 

A parser that can understand Valentina/Seamly2D formula, as seen, for example in 
the length attribute of drawing objects.

This outputs the formula as XML which represents the tree structure of the formula 
taking operator precedence into account, and recognising tokens as being numbers 
or references.

This was developed for https://seamly.cloud/ to assist with the parsing of uploaded 
patterns.


For instance:

```
formulaToXML( "a + 5 / 2" )
```

returns:

```
<?xml version="1.0" ?>
<operation type="add">
    <variable>a</variable>
    <operation type="divide">
        <integer>5</integer>
        <integer>2</integer>
  </operation>
</operation>
```
