export const saveRole = (roleList) => {
    localStorage.setItem("role", JSON.stringify(roleList));
};
export const getRole = () => {
    try {
        return JSON.parse(localStorage.getItem("roles")) || [];
    } catch {
        return [];
    }
};

export const hasRole = (role) => getRole().includes(role);
