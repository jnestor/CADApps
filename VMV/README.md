# VMV - Virtual Memory Visualization
This Java App provides a visualization of Virtual Memory to be used
for educational purposes.  You can run the program from a Linux shell
by typing the command:
```
  java -jar VirtualMemorySim.jar
  ```
Then click the "open" button and load a CSV configuration file.
See one of the CSV files in this directory to learn how to
set up a simulation.

![basic_layout_tlb.png]

## Configuration file Format
The configuration file is a CSV file that can be edited using a spreadsheet or text editor.  The first line is a header with a text description of each configuration value, followed by a second line containng the actual config value.  The configuration fields are as follows:
1. Size of RAM (physical memory) in pages.
2. Size of virtual address space in pages (poewr of 2).
3. Number of usable pages in physical RAM.
4. Number of usable pages in virtual memory space.
5. Page size in bytes
6. Include TLB (FALSE or TRUE)
7. TLB size (number of entries)
8. TLB replacement algorithm (FIFO or RANDOM or LRU)
9. Page replacement algorithm (CLOCK or FIFO or LFU or LRU)

The third line of the configuration file is a header for memory references describing the fields used in following memory references.  Memory references start in the fourth line with up to three fields:

1. Reference type (r or w).
2. Reference address (hexadecimal)
3. "SkipTil" field - simulation will automatically proceed until a reference is encountered containing the word "HERE".

Memory references may include comments starting with the "#" character.
