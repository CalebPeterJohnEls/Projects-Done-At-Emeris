document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("registration-form");
    const selectedCourses = document.getElementsByName("courses");
    const selectedCoursesDisplay = document.getElementById("selected-courses");
    const feeTableBody = document.getElementById("fee-table-body");
    const totalFeesDisplay = document.getElementById("total-fees-cell");
    const discountDisplay = document.getElementById("discount-cell");
    const totalAfterDiscountDisplay = document.getElementById("total-fees-after-discount-cell");

    Array.from(selectedCourses).forEach(course => {
        course.addEventListener("change", updateTable);
    });

    function updateTable() {
        let totalFees = 0;
        feeTableBody.innerHTML = '';

        const selectedCourseValues = Array.from(selectedCourses)
            .filter(course => course.checked)
            .map(course => course.value);

        selectedCourseValues.forEach(course => {
            let price = 0;
            switch (course) {
                case "first-aid":
                case "sewing":
                case "landscaping":
                case "life-skills":
                    price = 1500;
                    totalFees += 1500;
                    break;
                case "child-minding":
                case "cooking":
                case "garden-maintenance":
                    price = 750;
                    totalFees += 750;
                    break;
            }
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${course}</td>
                <td>R${price.toFixed(2)}</td>
            `;
            feeTableBody.appendChild(row);
        });

        let discount = 0;
        if (selectedCourseValues.length == 2) discount = 0.05;
        else if (selectedCourseValues.length == 3) discount = 0.10;
        else if (selectedCourseValues.length > 3) discount = 0.15;

        const totalDiscount = totalFees * discount;
        const totalAfterDiscount = totalFees - totalDiscount;

        totalFeesDisplay.textContent = `R${totalFees.toFixed(2)}`;
        discountDisplay.textContent = `R${totalDiscount.toFixed(2)}`;
        totalAfterDiscountDisplay.textContent = `R${totalAfterDiscount.toFixed(2)}`;
    }

    form.addEventListener("submit", function (event) {
        event.preventDefault();
    });
});
