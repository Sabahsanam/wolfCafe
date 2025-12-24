import React from "react";

const CustomerHomeComponent = () => {
    const [userUsername, setUserUsername] = React.useState("");

    React.useEffect(() => {
        const storedUsername =
            sessionStorage.getItem("authenticatedUser") || "";


        setUserUsername(storedUsername);

    }, []);


    return (
        <div className="min-h-full bg-background p-8 overscroll-contain flex flex-col">
            <div className="space-y-4">
                <h2 className="text-2xl font-semibold">Home</h2>
                <p className="text-muted-foreground">
                    Hello {userUsername}!
                </p>
            </div>
        </div>
    )
}
export default CustomerHomeComponent;
