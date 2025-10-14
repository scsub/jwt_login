export const setErrorMessage = (errors, setError) => {
    Object.entries(errors).forEach(([field, msg]) => {
        setError(field, { message: msg });
    });
};
