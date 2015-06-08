package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.interpreter.internal.special.SpecialFunctionEvaluator;
import de.benshu.cofi.runtime.Class;
import de.benshu.cofi.runtime.Instantiatable;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.Singleton;
import de.benshu.cofi.runtime.Trait;
import de.benshu.cofi.runtime.TypeDeclaration;
import de.benshu.cofi.types.ConstructedType;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

// TODO Split up/get rid of.
public class Utilities {
    private final ModuleInterpretation moduleInterpretation;

    public final CofiObject nil;
    public final CofiObject unit;

    Utilities(ModuleInterpretation moduleInterpretation) {
        this.moduleInterpretation = moduleInterpretation;

        this.nil = lookUpOrCreateSingleton("cofi", "lang", "Nil");
        this.unit = lookUpOrCreateSingleton("cofi", "lang", "Unit");
    }

    public boolean isTypeInvocationOf(CofiObject object, String... typeFqn) {
        final ProperType type = object.getType();

        if (!(type instanceof ConstructedType<?>))
            return false;

        final ProperTypeConstructor<?> constructor = lookUpType(typeFqn);
        final TypeList arguments = ((ConstructedType) type).getArguments();

        // TODO This could lead to invalidly kinded substitutions.. comparing constructors would be better
        return constructor.getParameters().size() == arguments.size()
                && AbstractConstraints.none().unbind().areEqualTypes(type, constructor.apply(arguments));
    }

    public boolean isOfExactType(CofiObject object, String... typeFqn) {
        return isExactType(object.getType(), typeFqn);
    }

    public boolean isExactType(ProperType type, Instantiatable instantiatable) {
        return isExactType(type, instantiatable.getType().applyTrivially());
    }

    public boolean isExactType(ProperType type, String... typeFqn) {
        return isExactType(type, lookUpInstantiable(typeFqn).getType().applyTrivially());
    }

    public boolean isExactType(ProperType type, TemplateType expectedType) {
        return AbstractConstraints.none().unbind().areEqualTypes(type, expectedType);
    }

    public ProperTypeConstructor<?> lookUpType(String... fqn) {
        return lookUpTypeDeclaration(fqn).getType();
    }

    public Instantiatable lookUpInstantiable(String... fqn) {
        return (Instantiatable) lookUpTypeDeclaration(fqn);
    }

    public Class lookUpClass(String... fqn) {
        return (Class) lookUpTypeDeclaration(fqn);
    }

    public Trait lookUpTrait(String... fqn) {
        return (Trait) lookUpTypeDeclaration(fqn);
    }

    public Singleton lookUpSingleton(String... fqn) {
        return lookUpSingleton(Fqn.from(fqn));
    }

    public Singleton lookUpSingleton(Fqn fqn) {
        final TypeDeclaration typeDeclaration = lookUpTypeDeclaration(fqn);
        return typeDeclaration instanceof Singleton
                ? (Singleton) typeDeclaration
                : (Singleton) typeDeclaration.getCompanion().get();
    }

    public TypeDeclaration lookUpTypeDeclaration(String... fqn) {
        return lookUpTypeDeclaration(Fqn.from(fqn));
    }

    public TypeDeclaration lookUpTypeDeclaration(Fqn fqn) {
        return (TypeDeclaration) moduleInterpretation.getFqnResolver().resolve(fqn);
    }

    public CofiObject lookUpOrCreateSingleton(String... fqn) {
        return moduleInterpretation.getSingletons().lookUpOrCreate(lookUpSingleton(fqn));
    }


    public int intFromNatural(CofiObject naturalLength) {
        final MemberDeclaration magDeclaration = naturalLength.getType().lookupMember("mag").get().getTags().get(MemberDeclaration.TAG);
        final CofiObject mag = naturalLength.getProperties().get(magDeclaration);
        final CofiObject int32 = mag.getSpecialValues().getInstance(CofiObject.class);
        return intFromInt32(int32);
    }

    public int intFromInt32(CofiObject int32) {
        return int32.getSpecialValues().getInstance(long.class).intValue();
    }

    public CofiObject createField(ProperType valueType, CofiObject initialValue) {
        final Class fieldImpDeclaration = lookUpClass("cofi", "lang", "FieldImpl");
        final TemplateType fieldImplType = fieldImpDeclaration.getType().apply(TypeList.of(valueType));

        final CofiObject field = new CofiObject(fieldImplType);
        field.getSpecialValues().putInstance(CofiObject.class, initialValue);
        return field;
    }

    public CofiObject createArray(ProperType elementType, CofiObject... elements) {
        final de.benshu.cofi.runtime.Class arrayDeclaration = lookUpClass("cofi", "lang", "collect", "Array");
        final TemplateType arrayType = arrayDeclaration.getType().apply(TypeList.of(elementType));

        final CofiObject result = new CofiObject(arrayType);
        result.getSpecialValues().put(CofiObject[].class, elements);
        return result;
    }

    public CofiObject naturalFrom(int value) {
        final Class in32Declaration = lookUpClass("cofi", "lang", "primitives", "Int32");
        final Class smallNaturalDeclaration = (Class) lookUpTypeDeclaration("cofi", "lang", "SmallNatural");

        final TemplateType in32Type = in32Declaration.getType().applyTrivially();

        return create(smallNaturalDeclaration, createPrimitive(in32Declaration, in32Type, value));
    }

    public CofiObject create(Class klazz, CofiObject... arguments) {
        return SpecialFunctionEvaluator.instanceCreateEvaluatorFor(moduleInterpretation, klazz)
                .evaluate(ImmutableList.copyOf(arguments));
    }

    public CofiObject createString(String value) {
        final TypeDeclaration charDeclaration = lookUpTypeDeclaration("cofi", "lang", "primitives", "Char");
        final Class stringDeclaration = (Class) lookUpTypeDeclaration("cofi", "lang", "String");

        final ProperType charType = charDeclaration.getType().applyTrivially();

        return create(stringDeclaration,
                createArray(charType, value.chars()
                        .mapToObj(moduleInterpretation.util::createChar)
                        .toArray(CofiObject[]::new))
        );
    }

    public CofiObject createIn32(int value) {
        return createPrimitive(value, "cofi", "lang", "primitives", "Int32");
    }

    public CofiObject createChar(int value) {
        return createPrimitive(value, "cofi", "lang", "primitives", "Char");
    }

    private CofiObject createPrimitive(long value, String... fqn) {
        final Class klazz = lookUpClass(fqn);
        return createPrimitive(klazz, klazz.getType().applyTrivially(), value);
    }

    private CofiObject createPrimitive(Class klazz, TemplateType type, long value) {
        final CofiObject result = new CofiObject(type);
        result.getSpecialValues().put(long.class, value);
        return result;
    }
}
