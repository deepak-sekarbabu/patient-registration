package com.deepak.logging.registration.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging method executions in specified packages (controllers, services, repositories).
 * This aspect uses Spring AOP's {@code @Around} advice to log method entry, arguments, return
 * values, execution time, and any exceptions thrown.
 *
 * <p>The pointcut expression has been configured to match the application's package structure
 * ({@code com.deepak.registration}).
 */
@Aspect
@Component
public class LoggingAspect {

  private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

  /**
   * Around advice that logs information about method execution. This includes the class name,
   * method name, arguments, execution time, method result (if successful), and any exceptions
   * thrown.
   *
   * <p>The pointcut expression targets all methods within the {@code
   * com.deepak.registration.service}, {@code com.deepak.registration.controller}, and {@code
   * com.deepak.registration.repository} packages.
   *
   * @param joinPoint The {@link ProceedingJoinPoint} representing the advised method.
   * @return The result of the advised method's execution.
   * @throws Throwable If the advised method throws an exception.
   */
  @Around(
      "execution(* com.deepak.registration.service..*(..)) || "
          + "execution(* com.deepak.registration.controller..*(..)) || "
          + "execution(* com.deepak.registration.repository..*(..))")
  public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = method.getName();
    Object[] methodArgs = joinPoint.getArgs();

    // Log method entry with class, method name, and arguments
    logger.info(
        "Enter: {}.{}() with argument[s] = {}", className, methodName, Arrays.toString(methodArgs));

    long startTime = System.currentTimeMillis(); // Record start time
    Object result;
    try {
      result = joinPoint.proceed(); // Execute the advised method
      long endTime = System.currentTimeMillis(); // Record end time

      // Log method exit with result and execution time
      logger.info(
          "Exit: {}.{}() with result = {}. Execution time = {} ms",
          className,
          methodName,
          result,
          (endTime - startTime));
      return result;
    } catch (Throwable throwable) {
      long endTime = System.currentTimeMillis(); // Record end time even in case of an exception

      // Log exception details with execution time
      logger.error(
          "Exception in {}.{}() with cause = '{}' and exception = '{}'. Execution time = {} ms",
          className,
          methodName,
          throwable.getCause() != null ? throwable.getCause() : "NULL",
          throwable.getMessage(),
          (endTime - startTime));
      throw throwable; // Re-throw the exception to maintain original behavior
    }
  }
}
