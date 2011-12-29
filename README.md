DAGExecutor
=============

An executor of Runnable Java objects. Each task is only executed after all tasks upon which it depends have finished. The dependencies are represented as a static directed-acyclic graph. Dynamically updated graphs are not yet supported.


Requirements
------------

This library requires some external libraries that are distributed in the "lib" directory of the repository.

* [guava](http://code.google.com/p/guava-libraries/) -- Currently used for some "google collections" support, namely for its MultiMap implementation.
* [junit](http://www.junit.org/) -- JUnit, only required for running unit tests


Contributing
------------

I'd love to incorporate changes from other contributors into this project.

1. Fork it.
2. Create a branch
3. Commit your changes
4. Push to the branch
5. Create an issue with a link to your branch
6. Enjoy a refreshing MountainDew Livewire and wait


Bug reports
-----------

If you encounter any bugs or unexpected behaviors that you would prefer not to fix on your own, please open an issue for tracking it.