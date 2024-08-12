# JSON with computed values

A personal experiment to see how would the JSON look like if it had 'views' aka computed values.
The expressions syntax is very simple limited to +, -, *, /, %, (, ), ==, !=, <, <=, >, >=, &&, || operators.
As for JSON it is full blown syntax with the extension for expressions.


## Example

One image is worth of thousand words... Let's say we have the json:

```json
{
  "fullName": "John Doe",
  "totalSalary": 1500,
  "isAdult": true,
  "tax": 0.18,
  "netIncome": 1230.0,
  "personal": {
    "firstName": "John",
    "lastName": "Doe",
    "age": 30,
    "jobSalary": 1000,
    "sideHustleSalary": 500
  }
}


```

With this lib we could write it as something like this:

```json
{
  "fullName": ${personal.firstName | " " | personal.lastName},
  "totalSalary": ${personal.jobSalary + personal.sideHustleSalary}
  "isAdult": ${personal.age >= 18},
  "tax": 0.18,
  "netIncome": ${(1.0 - tax) * totalSalary},
  "personal": {
    "firstName": "John",
    "lastName": "Doe",
    "age": 30,
    "jobSalary": 1000,
    "sideHustleSalary": 500,
    "totalSalary": "jobSalary + sideHustleSalary"
  }
}
```

Little thing but I but makes me happy, plus it was a good coding exercise.

## Couple of technical things

* The expressions are right associative which means that 1 + 2 + 3 becomes (1 + (2 + 3)). But the 
parentheses are fully supported. Expression implementation is a little bit sloppy as I had no time for that.
* Parsing JSON uses extensively pattern matching and extension methods which theoretically could 
cause problems with large JSONs. But the performance was not the aim of this project.
* Some algorithms are a little bit complicated but all corner cases are fully covered by tests.
* The lookup is implemented that that you specify the path.like.so It starts at the scope of definition and
iterates the parents. You can index into the array by specifying numbers in the path for example arr.1 .
* The implementation is not thread safe. It is not meant to be used in a multithreaded environment.
* It uses the tokenizer first and then the parser. It makes the parsing a little bit cleaner.
* The expression types are strict. Only conversion from Long to Double is possible. The left hand side determines the 
actual type of the expression factors.
* At first I had fun with it but later it became a little bit time consuming. But my programmers pride did not let me stop.
If I have more time some time in the future I will try to refector it a little bit.

