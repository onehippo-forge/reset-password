[![Build Status](https://travis-ci.org/onehippo-forge/reset-password.svg?branch=develop)](https://travis-ci.org/onehippo-forge/reset-password)

# Reset Password Plugin

The Reset Password plugin provides basic reset password functionality. 


# Documentation (GitHub Pages)

Documentation is available at [onehippo-forge.github.io/reset-password/](https://onehippo-forge.github.io/reset-password/).

You can generate the GitHub pages only from ```master``` branch by this command:

```bash
$ mvn -Pgithub.pages clean site
```

The output is in the ```/docs``` directory; push it and GitHub Pages will serve the site automatically. 

For rendering documentation on non-master branches, use the normal site command so the output will be in the ```/target``` 
and therefore ignored by Git.

 > mvn clean site:site