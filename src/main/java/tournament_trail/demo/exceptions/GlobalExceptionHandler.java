package tournament_trail.demo.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.BindException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ModelAndView handleInvalidVerificationToken(InvalidVerificationTokenException exception) {
        return new ModelAndView("redirect:/verification-failed?reason=invalid");
    }

    @ExceptionHandler(ExpiredVerificationTokenException.class)
    public ModelAndView handleExpiredVerificationToken(ExpiredVerificationTokenException exception) {
        return new ModelAndView("redirect:/verification-failed?reason=expired");
    }

    @ExceptionHandler({RegistrationReservationExpiredException.class
            , RequestAlreadyExistsException.class
            , IllegalStateException.class
            , IllegalArgumentException.class
            , MethodArgumentTypeMismatchException.class
            , MissingServletRequestParameterException.class
            , BindException.class
            , ConversionFailedException.class
            , AlreadyRegisteredException.class})
    public ModelAndView handle400Exception(Exception exception, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("error-400", HttpStatus.BAD_REQUEST);
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException exception
            , HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("error-403", HttpStatus.FORBIDDEN);
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("path", request.getRequestURI());

        return modelAndView;
    }

    @ExceptionHandler({InvalidCommentException.class
            , InvalidReviewException.class
            , InvalidTournamentRegistrationException.class
            , UserDoesNotExist.class
            , TournamentDoesNotExist.class
            , TravelGroupDoesNotExistException.class
            , NoResourceFoundException.class
            , NoHandlerFoundException.class})
    public ModelAndView handle404Exception(Exception exception, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("error-404", HttpStatus.NOT_FOUND);
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("path", request.getRequestURI());

        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpectedExceptions(Exception exception, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("error-500"
                , HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.addObject("message", "Something went wrong");
        modelAndView.addObject("path", request.getRequestURI());

        return modelAndView;
    }
}
