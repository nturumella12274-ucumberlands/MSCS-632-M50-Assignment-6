package main

import (
	"fmt"
	"log"
	"sync"
	"time"
)

func worker(id int, tasks <-chan int, wg *sync.WaitGroup, mu *sync.Mutex, results *[]string) {
	defer wg.Done()
	for task := range tasks {
		log.Printf("Worker %d started task %d\n", id, task)
		time.Sleep(1 * time.Second) // simulate processing delay
		result := fmt.Sprintf("Worker %d completed task %d", id, task)

		mu.Lock()
		*results = append(*results, result)
		mu.Unlock()

		log.Println(result)
	}
}

func main() {
	tasks := make(chan int, 20)
	var wg sync.WaitGroup
	var mu sync.Mutex
	var results []string

	// By default, log messages go to the console; there's no need to change the log output.

	// Add tasks
	for i := 1; i <= 20; i++ {
		tasks <- i
	}
	close(tasks)

	// Start 5 workers
	for i := 1; i <= 5; i++ {
		wg.Add(1)
		go worker(i, tasks, &wg, &mu, &results)
	}

	wg.Wait()

	// Output results to the console.
	fmt.Println("\nFinal results:")
	for _, res := range results {
		fmt.Println(res)
	}

	fmt.Println("\nAll tasks completed.")
}
