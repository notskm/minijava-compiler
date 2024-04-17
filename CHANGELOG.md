# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
- Improved symbol table for use in conversion to Vapor
- MethodTableVis class as first pass in conversion to Vapor
- VaporAST class for use in MiniJavaToVaporVis
- MiniJavaToVaporVis for compiling minijava to Vapor
- J2V class for compiling minijava to Vapor
- Ability to compile Factorial.java to Vapor

## [0.1.2]

### Fixed
- Failure to identify method overloading
- Failure to typecheck MoreThan4.java

## [0.1.1]

### Added
- TypecheckException class for cleaner error handling

### Fixed
- Ability to typecheck most provided minijava programs

## [0.1.0]

### Added
- Pretty printer for the minijava AST
- SymTableVis class for generating a symbol table
- Typechecker for minijava programs
- Ability to typecheck Factorial.java
