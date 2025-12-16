# Frida in the application

The application is limited by 3 ratings, with frida we're going to bypass this.

- Get process id (PID) using Frida: `frida-ps -Ua`
- Get the trial function out from of the running application with Frida-trace: `frida-trace -U -j "*!*Trial*" -p <pid>`
  - Explanation of the option j:  `classes!functions`, so * for classes is everything, and a wildcard around the functions to find every function with the word Trial in it when clicking on the rating.

Start Frida with `frida -U <pid>` and load the Javascript from the following repository: https://gitlab.ti.howest.be/ti/2025-2026/s3/mobilesecurity/students/group-17/BypassRatingWithFrida