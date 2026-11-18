package dev.mikoto2000.springboot.rfc9457;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class TaskServiceTest {
  @Test
  void completesAnOpenTaskAndRejectsAnotherCompletion() {
    TaskService service = new TaskService();
    Task completed = service.complete("t1");
    assertTrue(completed.completed());
    assertEquals(completed, service.find("t1"));
    assertEquals("資料を読む", completed.title());
    assertEquals("alice", completed.assignee());
    assertThrows(TaskAlreadyCompletedException.class, () -> service.complete("t1"));
  }

  @Test
  void rejectsMissingAndInitiallyCompletedTasks() {
    TaskService service = new TaskService();
    assertThrows(TaskNotFoundException.class, () -> service.complete("missing"));
    assertThrows(TaskNotFoundException.class, () -> service.find("missing"));
    assertThrows(TaskAlreadyCompletedException.class, () -> service.complete("t2"));
  }

  @RepeatedTest(20)
  @Timeout(15)
  void onlyOneConcurrentCompletionSucceeds() throws Exception {
    int callers = 32;
    TaskService service = new TaskService();
    CountDownLatch ready = new CountDownLatch(callers);
    CountDownLatch start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(callers)) {
      var results = new ArrayList<Future<Boolean>>();
      try {
        for (int i = 0; i < callers; i++) {
          results.add(executor.submit(() -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) {
              throw new AssertionError("Start signal timed out");
            }
            try {
              return service.complete("t1").completed();
            } catch (TaskAlreadyCompletedException expected) {
              return false;
            }
          }));
        }
        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        int successes = 0;
        for (Future<Boolean> result : results) {
          if (result.get(5, TimeUnit.SECONDS)) {
            successes++;
          }
        }
        assertEquals(1, successes);
        assertTrue(service.find("t1").completed());
      } finally {
        start.countDown();
        executor.shutdownNow();
      }
    }
  }
}
