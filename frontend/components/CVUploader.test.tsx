import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, vi } from "vitest";
import { CVUploader } from "./CVUploader";

const getCandidate = vi.fn();
const uploadCv = vi.fn();
vi.mock("@/lib/api", () => ({
  getCandidate: (...args: unknown[]) => getCandidate(...args),
  uploadCv: (...args: unknown[]) => uploadCv(...args),
}));

describe("CVUploader", () => {
  beforeEach(() => {
    getCandidate.mockReset(); uploadCv.mockReset();
    getCandidate.mockRejectedValue(new Error("No candidate profile exists."));
  });

  it("rejects a non-PDF before uploading", async () => {
    const user = userEvent.setup();
    render(<CVUploader />);
    await waitFor(() => expect(screen.getByText("No CV uploaded yet.")).toBeInTheDocument());
    const input = screen.getByLabelText("Choose PDF CV");
    fireEvent.change(input, { target: { files: [new File(["plain text"], "cv.txt", { type: "text/plain" })] } });
    expect(await screen.findByRole("alert")).toHaveTextContent("Please choose a PDF file.");
    expect(uploadCv).not.toHaveBeenCalled();
  });

  it("uploads a valid PDF and reports success", async () => {
    const user = userEvent.setup();
    uploadCv.mockResolvedValue({ id: "candidate", name: null, email: null, cvUploaded: true, updatedAt: "2026-08-15T08:00:00Z" });
    render(<CVUploader />);
    await waitFor(() => expect(screen.getByText("No CV uploaded yet.")).toBeInTheDocument());
    await user.upload(screen.getByLabelText("Choose PDF CV"), new File(["%PDF-test"], "cv.pdf", { type: "application/pdf" }));
    expect(await screen.findByRole("status")).toHaveTextContent("CV uploaded and text extracted successfully.");
    expect(uploadCv).toHaveBeenCalledOnce();
  });
});
