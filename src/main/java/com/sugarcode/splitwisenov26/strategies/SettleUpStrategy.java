package com.sugarcode.splitwisenov26.strategies;

import com.sugarcode.splitwisenov26.models.Expense;
import com.sugarcode.splitwisenov26.models.User;
import com.sugarcode.splitwisenov26.models.UserExpense;

import java.util.List;

public interface SettleUpStrategy {
    List<Expense> settleUp(List<UserExpense> userExpenses);
}
