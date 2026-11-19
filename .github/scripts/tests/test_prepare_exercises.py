"""Exercise generation must fail when a lesson loses its required tests."""

import importlib.util
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch


SCRIPT = Path(__file__).resolve().parents[1] / "prepare-exercises.py"
SPEC = importlib.util.spec_from_file_location("prepare_exercises", SCRIPT)
exercises = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(exercises)


class GenerationSafeguardsTest(unittest.TestCase):
    def test_missing_required_fixtures_fail_before_writing_projects(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with patch.object(exercises, "FIXTURES", root / "absent-fixtures"):
                for lesson in exercises.FIXTURE_LESSONS:
                    with self.subTest(lesson=lesson):
                        project = root / lesson
                        with self.assertRaisesRegex(FileNotFoundError, "required test fixture"):
                            exercises.prepare_additional(project, lesson)
                        self.assertFalse(project.exists())

    def test_every_additional_lesson_generates_test_sources(self):
        with tempfile.TemporaryDirectory() as directory:
            for lesson in exercises.ADDITIONAL_LESSONS:
                with self.subTest(lesson=lesson):
                    project = Path(directory) / lesson
                    exercises.prepare_additional(project, lesson)
                    self.assertTrue(list((project / "src/test/java").rglob("*Test.java")))

    def test_old_python_is_rejected_before_parsing_arguments(self):
        with patch.object(sys, "version_info", (3, 9, 0)):
            with self.assertRaisesRegex(SystemExit, "Python 3.10 or newer"):
                exercises.main()


if __name__ == "__main__":
    unittest.main()
