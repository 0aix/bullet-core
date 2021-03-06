package com.yahoo.bullet.querying.evaluators;

import com.yahoo.bullet.parsing.expressions.ListExpression;
import com.yahoo.bullet.record.BulletRecord;
import com.yahoo.bullet.typesystem.TypedObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluator that evaluates a list of evaluators on a BulletRecord and then returns the list of results (after casting them).
 */
public class ListEvaluator extends Evaluator {
    private List<Evaluator> evaluators;

    public ListEvaluator(ListExpression listExpression) {
        super(listExpression);
        this.evaluators = listExpression.getValues().stream().map(Evaluator::build).collect(Collectors.toList());
    }

    @Override
    public TypedObject evaluate(BulletRecord record) {
        //TypedObject result = new TypedObject(Type.LIST, evaluators.stream().map(e -> e.evaluate(record)).collect(Collectors.toList()));
        //return cast(result);
        return new TypedObject(evaluators.stream().map(e -> e.evaluate(record).forceCast(type).getValue()).collect(Collectors.toList()));
    }
}
