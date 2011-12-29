DAGExecutor
=============

An executor of Runnable Java objects. Each task is only executed after all tasks upon which it depends have finished. The dependencies are represented as a static directed-acyclic graph. Dynamically updated graphs are not yet supported.


Requirements
------------

This library requires some external libraries:

* [guava](http://code.google.com/p/guava-libraries/) -- Currently used for some collections support.
* [junit](http://www.junit.org/) -- JUnit for testing


Contributing
------------

I'd love to incorporate changes from other contributors into this project.

1. Fork it.
2. Create a branch
3. Commit your changes
4. Push to the branch
5. Create an [Issue][1] with a link to your branch
6. Enjoy a refreshing Mountain Dew Livewire and wait
