package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcQuizDao implements QuizDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcQuizDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public List<Quiz> getQuestionsByQuizId(int quizId){
        List<Quiz> questionList = new ArrayList<>();
        String sql =
                "SELECT * FROM questions "
        +"Where quiz_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, quizId);
            while(results.next()) {
                Quiz quiz = mapRowToQuiz(results);
                questionList.add(quiz);
            }
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot find list of quizzes");
        }
        return questionList;
    }


    @Override
    public boolean createQuiz(CreateQuizDTO quiz, int userId) {
        String insertQuizSql = "INSERT INTO quizzes (quiz_name, category_id) VALUES (?, ?) RETURNING quiz_id";

        try {
            int newQuizId = jdbcTemplate.queryForObject(insertQuizSql, int.class, quiz.getQuizName(), quiz.getCategoryId());

            if (newQuizId > 0) {
                String insertQuestionSql = "INSERT INTO questions (quiz_id, question, answer) VALUES (?, ?, ?)";
                int rowsAffected = 0;

                for (QuestionAnswerDTO qa : quiz.getQuestionAnswers()) {
                    int result = jdbcTemplate.update(insertQuestionSql, newQuizId, qa.getQuestion(), qa.getAnswer());
                    if (result > 0) {
                        rowsAffected++;
                    }
                }

                // Insert user-quiz association
                String insertUserQuizSql = "INSERT INTO users_quizzes (user_id, quiz_id) VALUES (?, ?)";
                int result2 = jdbcTemplate.update(insertUserQuizSql, userId, newQuizId);

                // Check if all inserts were successful
                if (rowsAffected == quiz.getQuestionAnswers().size() && result2 > 0) {
                    return true;
                }
            }
        } catch (DataAccessException e) {
            throw new DaoException("Could not edit Quiz", e);
        }
        return false;
    }

    @Override
    public boolean updateQuiz(UpdateQuizDTO updatedQuiz, int quizId) {
        String updateQuizSql = "UPDATE quizzes SET quiz_name = ?, category_id = ? WHERE quiz_id = ?";
        String deleteQuestionsSql = "DELETE FROM questions WHERE quiz_id = ?";
        String insertQuestionSql = "INSERT INTO questions (quiz_id, question, answer) VALUES (?, ?, ?)";

        try {
            // Update the quiz information
            int rowsUpdated = jdbcTemplate.update(updateQuizSql, updatedQuiz.getQuizName(), updatedQuiz.getCategoryId(), quizId);

            // Delete existing questions for the quiz
            jdbcTemplate.update(deleteQuestionsSql, quizId);

            // Insert updated questions
            int rowsAffected = 0;
            for (QuestionAnswerDTO qa : updatedQuiz.getQuestionAnswers()) {
                int result = jdbcTemplate.update(insertQuestionSql, quizId, qa.getQuestion(), qa.getAnswer());
                if (result > 0) {
                    rowsAffected++;
                }
            }

            // Check if the update and insert operations were successful
            if (rowsUpdated > 0 && rowsAffected == updatedQuiz.getQuestionAnswers().size()) {
                return true;
            }
        } catch (DataAccessException e) {
            throw new DaoException("Could not update Quiz", e);
        }
        return false;
    }


@Override
public boolean deleteQuizByQuizId(int quizId){
    String questionSql = "DELETE FROM questions WHERE quiz_id=?";
    String userQuizSql = "DELETE FROM users_quizzes WHERE quiz_id =?";
    String sql = "DELETE FROM quizzes WHERE quiz_id = ?";
    try{
        jdbcTemplate.update(questionSql, quizId);
        jdbcTemplate.update(userQuizSql, quizId);
        int rowsAffected = jdbcTemplate.update( sql,quizId);
        return rowsAffected != 0;
    } catch (DataAccessException e) {
        throw new DaoException("Cannot delete quiz", e);
    }
};

    private Quiz mapRowToQuiz(SqlRowSet rs){
        Quiz quiz = new Quiz();
        quiz.setQuestionId(rs.getInt("question_id"));
        quiz.setQuizId(rs.getInt("quiz_id"));
        quiz.setQuestion(rs.getString("question"));
        quiz.setAnswer(rs.getString("answer"));

        return quiz;
    }
    //TODO see if you can consolidat the Jdbcs for quiz and quizList,
    // i kept them separate because I got invalide column names when i
    // included them in the select for getting the quizLists

}
