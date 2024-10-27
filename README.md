# Stock Exchange Simulation

This is a project aimed at simulating different investing strategies on Stock Exchange. Currently the simulation has only two strategies, which is a random investor (self explanatory) and an SMA investor, which uses the Simple Moving Average
to calculate his next move. The program is designed so that adding new investing strategies is straightforward.

There are a few Stock Order types:
- Immediate -> Has to be considered in the current round or else gets deleted
- Persistent -> Stays in the Stock Exchange for as long as it needs to
- Due -> Has an expiration date
- Full Execution -> Has to be fulfilled entirely in a single round, or else it gets removed.

The simulation has some limitations, mostly to how the Full Execution order is calculated, as it turns out to be really tricky to account for them when multiple orders of this type may occur and a pretty nasty recurrence arrises.
Still, the program is flexible enough to observe how different investing strategies might work on the stock exchange.
The input to this program is a simple file with format specified in the example file. It basically sets the number of different investors, the stocks available and the starting wallets for investors.
