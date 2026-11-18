package com.sugarcode.splitwisenov26.strategies;

import com.sugarcode.splitwisenov26.models.Expense;
import com.sugarcode.splitwisenov26.models.User;
import com.sugarcode.splitwisenov26.models.UserExpense;
import com.sugarcode.splitwisenov26.models.enums.ExpenseType;
import com.sugarcode.splitwisenov26.models.enums.UserExpenseType;

import java.util.*;

public class HeapSettleUpStrategy implements SettleUpStrategy {
    public List<Expense> settleUp(List<UserExpense> userExpenses) {
        Map<Long, Double> balances = new HashMap<>();
        Map<Long, User> users = new HashMap<>();

        //Created the balances for each user
//        for (Expense expense : expenses) { //E1
//            /*
//            Expense(E1) -> Dinner --> 2000
//
//            u1, u2, u3, u4
//
//            PaidBy <E1, U1, 800>; <E1, U3, 1200>
//            HadToPay <E1, U1, 500>; <E1, U2, 500>; <E1, U3, 500>; <E1, U4, 500>
//             */
            for(UserExpense userExpense : userExpenses){
                User user = userExpense.getUser();
                Long userId = user.getId();
                users.put(userId, user);
                // u1, u2, u3, u4


                if(userExpense.getExpense().getExpenseType() != ExpenseType.ACTUAL){
                    continue;
                }


                balances.putIfAbsent(userId, 0.0);
                if(userExpense.getUserExpenseType() == UserExpenseType.PAID_BY){
                    balances.put(userId, balances.get(userId)+userExpense.getAmount());
                } else if (userExpense.getUserExpenseType() == UserExpenseType.HAD_TO_PAY) {
                    balances.put(userId, balances.get(userId)-userExpense.getAmount());
                }

            }
//        }

        //create heaps
        //debitors --> hadtopay
        PriorityQueue<Map.Entry<Long, Double>> minHeap = new PriorityQueue<>(Map.Entry.comparingByValue());
        PriorityQueue<Map.Entry<Long, Double>> maxHeap = new PriorityQueue<>(Map.Entry.<Long, Double>comparingByValue().reversed());

        //populate the heaps
        for(Map.Entry<Long, Double> entry : balances.entrySet()){
            if(entry.getValue() <0 ){
                minHeap.offer(entry); //all debitors
            }
            else{
                maxHeap.offer(entry);// all creditors
            }
        }

        //Settlement transactions
        List<Expense>  settlementExpenses = new ArrayList<>();
        while(!minHeap.isEmpty() && !maxHeap.isEmpty()){
            Map.Entry<Long, Double> maxCreditor = maxHeap.poll();//+8200
            Map.Entry<Long, Double> minDebitor = minHeap.poll();//-4400

            //calculate the amount
            //u1 --> +8200 , u2--> -4400
            double amount = Math.min(maxCreditor.getValue(),-minDebitor.getValue());


            //Fake dinner -- > expense -- 4400
            Expense settlement = new Expense();

            settlement.setDescription("Settlement Expense "+ minDebitor.getKey()+" paid amount of"+ amount +" to " + maxCreditor.getKey() );
            settlement.setAmount(amount);
            settlement.setCreatedBy(users.get(minDebitor.getKey()));
            settlement.setExpenseType(ExpenseType.SETTLEMENT);

            //paidBy  ---> u2 -- 4400
            //hadtopay --> u1 -- -4400
            //create a userExepense records
            UserExpense paidBy = new UserExpense();
            paidBy.setUser(users.get(minDebitor.getKey()));
            paidBy.setAmount(amount);
            paidBy.setUserExpenseType(UserExpenseType.PAID_BY);
            paidBy.setExpense(settlement);

            UserExpense hadToPay = new UserExpense();
            hadToPay.setUser(users.get(maxCreditor.getKey()));
            hadToPay.setAmount(amount);
            hadToPay.setUserExpenseType(UserExpenseType.HAD_TO_PAY);
            hadToPay.setExpense(settlement);

            settlement.setUserExpenses(new ArrayList<>(List.of(hadToPay, paidBy)));

            settlementExpenses.add(settlement);

            //update the balances
            double debitorRemaining = minDebitor.getValue() + amount;  //u2 to give 4400 but paid for fake dinner
            double creditorRemaining = maxCreditor.getValue() - amount; //u1 to get 8200 but had to pay fake dinner

            if(debitorRemaining < 0){
                minHeap.offer(
                        new AbstractMap.SimpleEntry<>( //Map.entry is interface like list and Abstract.simpleEntry is implementation like Arraylist
                                minDebitor.getKey(),
                                debitorRemaining
                        )
                );
            }
            if(creditorRemaining > 0){
                maxHeap.offer(
                        new AbstractMap.SimpleEntry<>(
                                maxCreditor.getKey(),
                                creditorRemaining
                        )
                );
            }
        }
        return settlementExpenses;
    }
}
/*
1.userExpenses are given to us
2.get all the users involved from userExpenses
3.calculate the balance for each user
4.add debitors(negative balance) to minheap and creditors to maxheap
5.fetch the maxCreditor and minDebitor
6.calculate the amount to be transferred b/w them
7.Create a fake/settlement expense
8.Create userExpenses for this expense
9. add this userExpense to the expense
10. update the maxCreditor and minDebitor balances in their respective heaps
11. return the list of expenses from which user has to get or user has to pay
 */
