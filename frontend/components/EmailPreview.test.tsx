import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { EmailPreview } from "./EmailPreview";

describe("EmailPreview", () => {
  it("allows editing and copies the complete draft", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const writeText = vi.fn().mockResolvedValue(undefined);
    Object.defineProperty(navigator, "clipboard", { configurable: true, value: { writeText } });
    render(<EmailPreview email={{ subject: "Internship inquiry", body: "Dear Hiring Team," }} onChange={onChange} />);

    await user.clear(screen.getByLabelText("Subject"));
    await user.type(screen.getByLabelText("Subject"), "Updated subject");
    expect(onChange).toHaveBeenCalled();

    await user.click(screen.getByRole("button", { name: "Copy full email" }));
    expect(writeText).toHaveBeenCalledWith("Subject: Internship inquiry\n\nDear Hiring Team,");
    expect(screen.getByRole("button", { name: "Copied email" })).toBeInTheDocument();
  });
});
